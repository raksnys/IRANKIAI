import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgFor, NgClass, NgIf } from '@angular/common'; // Corrected: Import NgFor, NgClass, NgIf from @angular/common
import { FormsModule } from '@angular/forms'; // FormsModule is correctly from @angular/forms
import { environment } from '../environments/environment'; // Assuming this path is correct for your project structure
import { interval, Subscription } from 'rxjs';

interface GridCell {
  x: number;
  y: number;
  z: number;
  type: 'ROBOT' | 'CONTAINER' | 'CHARGING_STATION' | 'CACHE' | 'COLLECT_ORDER' | 'DELIVER_ORDER';
  entityId: number;
}

// Define Product interface if not already defined elsewhere
interface Product {
  id: number;
  name: string;
  price: number;
  // Add other product properties as needed, e.g.:
  // weight?: number;
  // dimensions?: string;
}

// Define Container interface for better type safety if not already defined
interface Container {
  id: number;
  location: { x: number; y: number; z: number };
  products: Product[]; // Assuming a container can have products
}

// It's good practice to define an interface for your DTO
interface ProductDeliveryRequestDTO {
  productId: number;
  quantity: number;
  targetContainerId: number;
}

@Component({
  selector: 'app-telemetry',
  imports: [
    NgFor, // Should now be correctly resolved
    FormsModule, 
    NgClass, // Should now be correctly resolved
    NgIf // Should now be correctly resolved
  ],
  templateUrl: './telemetry.component.html',
  styleUrl: './telemetry.component.scss',
  standalone: true
})
export class TelemetryComponent implements OnInit, OnDestroy {
  gridCells: GridCell[] = [];
  private refreshSubscription: Subscription | null = null;
  private currentType: GridCell['type'] | undefined;
  selectedRobot: any = null; // Consider using a specific Robot interface
  selectedContainer: Container | null = null; // Use the Container interface

  // New properties for "Add Product" functionality
  showProductSelection: boolean = false;
  availableProducts: Product[] = [];
  selectedProductIdForContainer: number | null = null;
  deliveryQuantityForSelectedProduct: number | null = 1; // Default to 1 or null as you prefer
  loadingProducts: boolean = false;


  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchGridData();

    this.refreshSubscription = interval(460).subscribe(() => {
      this.fetchGridData(this.currentType);
    });
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  fetchGridData(type?: GridCell['type']): void {
    this.currentType = type;

    const url = type
      ? `${environment.apiUrl}/grid/byType?type=${type}`
      : `${environment.apiUrl}/grid/byType`;

    this.http.get<GridCell[]>(url)
      .subscribe({
        next: (data) => {
          this.gridCells = data;
        },
        error: (error) => {
          console.error('Error fetching grid data:', error);
        }
      });
  }

  addCell(x: number, y: number, type: string): void {
    const cellType = type as GridCell['type'];

    const gridLocation = { x, y, z: 0 };

    let endpoint = '';
    let payload = {};

    switch (cellType) {
      case 'CONTAINER':
        endpoint = `${environment.apiUrl}/container`;
        payload = {
          location: gridLocation,
          products: []
        };
        break;
      case 'ROBOT':
        endpoint = `${environment.apiUrl}/robot`;
        payload = {
          location: gridLocation,
          batteryLevel: 100
        };
        break;
      case 'CHARGING_STATION':
        endpoint = `${environment.apiUrl}/chargingStation`;
        payload = {
          location: gridLocation
        };
        break;
      case 'CACHE':
        endpoint = `${environment.apiUrl}/cache`;
        payload = {
          location: gridLocation
        };
        break;
      case 'COLLECT_ORDER':
        endpoint = `${environment.apiUrl}/collectOrder`;
        payload = {
          location: gridLocation
        };
        break;
        case 'DELIVER_ORDER':
          endpoint = `${environment.apiUrl}/deliverOrder`;
          payload = {
            location: gridLocation
          };
          break;
      // TODO: Pridet likusius cases...
      default:
        console.error(`Unsupported type: ${cellType}`);
        return;
    }

    this.http.post(endpoint, payload).subscribe({
      next: (response) => {
        console.log(`Added ${cellType} at position (${x},${y})`);
        this.fetchGridData();
      },
      error: (error) => {
        console.error(`Error adding ${cellType}:`, error);
      }
    });
  }
  handleCellClick(cell: GridCell): void {
    switch(cell.type) {
      case 'ROBOT':
        this.showRobotDetails(cell.entityId);
        break;
      case 'CONTAINER':
        this.showContainerDetails(cell.entityId);
        break;
      // Add more cases if you want to show details for other entity types
    }
  }
  
  showRobotDetails(robotId: number): void {
    this.http.get(`${environment.apiUrl}/robot?id=${robotId}`).subscribe({
      next: (robot: any) => {
        this.selectedRobot = robot;
        this.selectedContainer = null; // Close container modal if open
        this.resetProductSelectionState();
      },
      error: (error) => {
        console.error('Error fetching robot details:', error);
      }
    });
  }
  
  closeModal(): void {
    this.selectedRobot = null;
  }
  
  showContainerDetails(containerId: number): void {
    this.http.get<Container>(`${environment.apiUrl}/container?id=${containerId}`).subscribe({
      next: (containerDetails: Container) => {
        this.selectedContainer = containerDetails;
        this.selectedRobot = null; // Close robot modal if open
        this.resetProductSelectionState(); // Reset product selection when opening a new container
      },
      error: (error) => {
        console.error('Error fetching container details:', error);
        this.selectedContainer = null; // Ensure it's null on error
        this.resetProductSelectionState();
      }
    });
  }
  
  closeContainerModal(): void {
    this.selectedContainer = null;
    this.resetProductSelectionState();
  }

  private resetProductSelectionState(): void {
    this.showProductSelection = false;
    this.availableProducts = [];
    this.selectedProductIdForContainer = null;
    this.deliveryQuantityForSelectedProduct = 1; // Reset to default value
    this.loadingProducts = false;
  }

  toggleProductSelection(): void {
    this.showProductSelection = !this.showProductSelection;
    if (this.showProductSelection && this.availableProducts.length === 0) {
      this.fetchAvailableProducts();
    } else if (!this.showProductSelection) {
      // Optionally reset selected product when hiding the selection
      this.selectedProductIdForContainer = null;
    }
  }

  fetchAvailableProducts(): void {
    this.loadingProducts = true;
    // Ensure you have a /product endpoint or adjust as necessary
    this.http.get<Product[]>(`${environment.apiUrl}/products`).subscribe({
      next: (products) => {
        this.availableProducts = products;
        this.loadingProducts = false;
      },
      error: (err) => {
        console.error('Error fetching available products:', err);
        this.availableProducts = []; // Clear products on error
        this.loadingProducts = false;
      }
    });
  }

  submitAddProductToSelectedContainer(): void {
    if (!this.selectedContainer || this.selectedProductIdForContainer === null) {
      console.error('Container or Product ID is missing.');
      alert('Please select a container and a product.');
      return;
    }

    if (!this.deliveryQuantityForSelectedProduct || this.deliveryQuantityForSelectedProduct < 1) {
      alert('Please enter a valid quantity (minimum 1).');
      return;
    }

    const payload: ProductDeliveryRequestDTO = {
      productId: this.selectedProductIdForContainer,
      quantity: this.deliveryQuantityForSelectedProduct,
      targetContainerId: this.selectedContainer.id // Assuming selectedContainer has an 'id'
    };

    this.http.post<any>(`${environment.apiUrl}/tasks/request-product-delivery`, payload).subscribe({
      next: (response) => {
        console.log('Product delivery task created successfully:', response);
        alert(`Robot task created to deliver the product! Task ID: ${response?.id || 'N/A'}`);
        this.closeContainerModal(); // Or other UI updates
      },
      error: (err) => {
        console.error('Error creating product delivery task:', err);
        // Display a more informative error message from the backend if available
        const errorMessage = err.error?.message || err.error?.error || err.message || 'Failed to create robot task. Please try again.';
        alert(errorMessage);
      }
    });
  }
}