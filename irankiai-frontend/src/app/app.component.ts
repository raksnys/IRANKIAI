import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { NavbarComponent } from './navbar/navbar.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    NavbarComponent,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Programų sistemų analizės ir projektavimo įrankiai (T120B029)';
}
