import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';


interface GridCell{
  x: number;
  y: number;
  type: 'ROBOT' | 'CONTAINER' | 'CHARGING_STATION' | 'CACHE' | 'COLLECT_ORDER' | 'DELIVER_ORDER';
}


@Component({
  selector: 'app-telemetry',
  imports: [NgFor, FormsModule],
  templateUrl: './telemetry.component.html',
  styleUrl: './telemetry.component.scss'
})
export class TelemetryComponent {
  gridCells: GridCell[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.addCell(0, 0, 'ROBOT');
    this.addCell(5, 5, 'CHARGING_STATION');
    this.addCell(7, 7, 'CACHE');
    this.addCell(10, 10, 'COLLECT_ORDER');
    this.addCell(32, 32, 'DELIVER_ORDER');
    this.addCell(0, 18, 'ROBOT');
  
    this.fetchGridData();
  }

  fetchGridData(type?: GridCell['type']): void{
    const url = type  
    ? `${environment.apiUrl}/grid?type=${type}`
    : `${environment.apiUrl}/grid`;
    
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
    const existingIndex = this.gridCells.findIndex(cell => cell.x === x && cell.y === y);
  
  if (existingIndex !== -1) {
    // Update existing cell
    this.gridCells[existingIndex].type = cellType;
  } else {
    // Add new cell
    this.gridCells.push({ x, y, type: cellType });
  }
  }

}
