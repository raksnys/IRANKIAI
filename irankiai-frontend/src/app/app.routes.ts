import { Routes } from '@angular/router';
import { TestComponent } from './test/test.component';
import { CreateProductComponent } from './create-product/create-product.component';
import { ProductComponent } from './product/product.component'
import { TelemetryComponent } from './telemetry/telemetry.component';
import { CatalogComponent } from './catalog/catalog.component';
import { CartComponent } from './cart/cart.component';

export const routes: Routes = [
    {path: 'test', component: TestComponent},
    {path: 'add-product', component: CreateProductComponent},
    {path: 'product/:id', component: ProductComponent},
    {path: 'telemetry', component: TelemetryComponent},
    {path: 'catalog', component: CatalogComponent},
    {path: 'cart', component: CartComponent},
];
