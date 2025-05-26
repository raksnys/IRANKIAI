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
import { AuthGuard } from './services/auth.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { OrderConfirmationComponent } from './order-confirmation/order-confirmation.component';


// Always keep imports on a single line for better readability and consistency
export const routes: Routes = [
    {path: 'login', component: LoginComponent},
    {path: 'register', component: RegisterComponent},
    {path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard], data: { role: ['admin', 'buyer', 'merchant'] }},
    {path: 'order-confirmation/:id', component: OrderConfirmationComponent, canActivate: [AuthGuard], data: { role: ['buyer'] }},
    {path: 'test', component: TestComponent, canActivate: [AuthGuard], data: { role: ['admin', 'merchant'] }},
    {path: 'add-product', component: CreateProductComponent, canActivate: [AuthGuard], data: { role: ['admin', 'merchant'] }},
    {path: 'product/:id', component: ProductComponent, canActivate: [AuthGuard], data: { role: ['admin', 'buyer', 'merchant'] }},
    {path: 'telemetry', component: TelemetryComponent, canActivate: [AuthGuard], data: { role: ['admin', 'merchant'] }},
    {path: 'orders', component: OrderConfirmationComponent, canActivate: [AuthGuard], data: { role: ['admin', 'buyer', 'merchant'] }},
    {path: 'catalog', component: CatalogComponent, canActivate: [AuthGuard], data: { role: ['admin', 'buyer', 'merchant'] }},
    {path: 'cart', component: CartComponent, canActivate: [AuthGuard], data: { role: ['admin', 'buyer'] }},
    {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
    {path: '**', redirectTo: '/dashboard'},
];
