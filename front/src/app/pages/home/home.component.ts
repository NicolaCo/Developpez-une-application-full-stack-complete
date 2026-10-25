import { Component, inject } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/service/auth.service';
import { FeedComponent } from '../../components/feed/feed.component';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [MatButton, RouterLink, FeedComponent]
})
export class HomeComponent {
  private authService = inject(AuthService);

  protected readonly isLoggedIn = this.authService.isLoggedIn;
}
