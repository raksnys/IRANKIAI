import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgFor, NgClass, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../environments/environment';
import { interval, Subscription } from 'rxjs';

interface GridCell {
  x: number;
  y: number;
  z: number;
  type: 'ROBOT' | 'CONTAINER' | 'CHARGING_STATION' | 'CACHE' | 'COLLECT_ORDER' | 'DELIVER_ORDER';
}

@Component({
  selector: 'app-telemetry',
  imports: [NgFor, FormsModule, NgClass, NgIf],
  templateUrl: './telemetry.component.html',
  styleUrl: './telemetry.component.scss',
  standalone: true
})
export class TelemetryComponent implements OnInit, OnDestroy {
  gridCells: GridCell[] = [];
  private refreshSubscription: Subscription | null = null;
  private currentType: GridCell['type'] | undefined;


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
}