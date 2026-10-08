import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [MatButton]
})
export class HomeComponent implements OnInit {
  constructor() {
    // TODO Remove if empty
  }

  ngOnInit(): void {
    // TODO Remove if empty
  }

  start() {
    alert('Commencez par lire le README et à vous de jouer !');
  }
}
