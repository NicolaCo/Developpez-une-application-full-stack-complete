import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MaterialModule } from '../../../shared/material.module';
import { Topic } from '../../core/model/topic.interface';

@Component({
  imports: [MaterialModule],
  selector: 'app-topicscard',
  styleUrl: './topiccard.component.scss',
  templateUrl: './topiccard.component.html',
})
export class TopiccardComponent {
  @Input({ required: true }) topic!: Topic;
  @Output() subscribe = new EventEmitter<Topic>();
  @Output() detail = new EventEmitter<Topic>();

  protected onSubscribe(): void {
    this.subscribe.emit(this.topic);
  }

  protected onDetail(): void {
    this.detail.emit(this.topic);
  }
}
