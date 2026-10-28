import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TopicService } from '../../core/service/topic.service';
import { Topic } from '../../core/model/topic.interface';
import { MaterialModule } from '../../../shared/material.module';
import { TopiccardComponent } from '../../components/topicscard/topiccard.component';
import { Subject, startWith, switchMap } from 'rxjs';

@Component({
  imports: [MaterialModule, TopiccardComponent],
  selector: 'app-topics',
  styleUrl: './topics.component.scss',
  templateUrl: './topics.component.html',
})
export class TopicsComponent {

  private topicService = inject(TopicService);
  private refresh$ = new Subject<void>();

  protected readonly topicsList = toSignal(
    this.refresh$.pipe(startWith(void 0), switchMap(() => this.topicService.all())),
    { initialValue: [] as Topic[] }
  );

  protected onSubscribe(topic: Topic): void {
    const obs = topic.isSubscribed
      ? this.topicService.unsubscribe(topic.id)
      : this.topicService.subscribe(topic.id);
    obs.subscribe(() => this.refresh$.next());
  }

  protected onDetail(): void {
    return;
  }

}
