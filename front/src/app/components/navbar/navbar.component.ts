import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../core/service/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [MaterialModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  protected readonly isLoggedIn = this.authService.isLoggedIn;

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}