import { Component } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [MatButton, RouterLink]
})
export class HomeComponent {
  constructor() {
    // TODO Remove if empty
  }

    // TODO : implémenter ngOnInit(): void {} si besoin

}
