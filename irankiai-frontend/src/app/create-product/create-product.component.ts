import { Component, OnInit } from '@angular/core';
import { environment } from '../environments/environment';
import { FormGroup, ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-product',
  imports: [ReactiveFormsModule],
  templateUrl: './create-product.component.html',
  styleUrl: './create-product.component.scss'
})
export class CreateProductComponent implements OnInit {
  form !: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      name: '',
      dimensions: '',
      weight: 0,
      price: 0,
      color: ''
    });
  }

  onSubmit(): void {
    this.http.post<any>(`${environment.apiUrl}/product`, this.form.value)
      .subscribe(
        (response) => {
          // Navigate to the product details page with the new product ID
          this.router.navigate(['/product', response.id]);
        },
        error => console.error(error)
      );
  }
}
