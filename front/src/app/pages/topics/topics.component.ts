import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TopicService } from '../../core/service/topic.service';
import { Topic } from '../../core/model/topic.interface';
import { MaterialModule } from '../../../shared/material.module';
import { TopiccardComponent } from '../../components/topicscard/topiccard.component';

@Component({
  imports: [MaterialModule, TopiccardComponent],
  selector: 'app-topics',
  styleUrl: './topics.component.scss',
  templateUrl: './topics.component.html',
})
export class TopicsComponent {

  private topicService = inject(TopicService);

  protected readonly topicsList = toSignal(this.topicService.all(), { initialValue: [] as Topic[] });

  protected onSubscribe(topic: Topic): void {
    // Logic to be implemented
  }

  protected onDetail(topic: Topic): void {
    // Logic to be implemented
  }

}
