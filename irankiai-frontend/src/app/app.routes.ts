import { Routes } from '@angular/router';
import { TestComponent } from './test/test.component';
import { CreateProductComponent } from './create-product/create-product.component';
import { ProductComponent } from './product/product.component'
import { TelemetryComponent } from './telemetry/telemetry.component';
import { CatalogComponent } from './catalog/catalog.component';
import { CartComponent } from './cart/cart.component';
import { LoginComponent } from './login.component';
import { RegisterComponent } from './register.component';
import { AdminComponent } from './admin.component';
import { BuyerComponent } from './buyer.component';
import { MerchantComponent } from './merchant.component';
import { UserComponent } from './user.component';

export const routes: Routes = [
    {path: 'login', component: LoginComponent},
    {path: 'register', component: RegisterComponent},
    {path: 'admin', component: AdminComponent},
    {path: 'buyer', component: BuyerComponent},
    {path: 'merchant', component: MerchantComponent},
    {path: 'user', component: UserComponent},
    {path: 'test', component: TestComponent},
    {path: 'add-product', component: CreateProductComponent},
    {path: 'product/:id', component: ProductComponent},
    {path: 'telemetry', component: TelemetryComponent},
    {path: 'catalog', component: CatalogComponent},
    {path: 'cart', component: CartComponent},
    {path: '', redirectTo: '/login', pathMatch: 'full'},
    {path: '**', redirectTo: '/login'},
];
