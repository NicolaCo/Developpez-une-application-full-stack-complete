import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Topic } from '../model/topic.interface';

@Injectable({
  providedIn: 'root'
})
export class TopicService {

  private pathService = environment.apiUrl + 'topics';

  private httpClient = inject(HttpClient);

  public all(): Observable<Topic[]> {
    return this.httpClient.get<Topic[]>(this.pathService);
  }

  public subscribe(id: number): Observable<void> {
    return this.httpClient.post<void>(`${this.pathService}/${id}/subscribe`, {});
  }

  public unsubscribe(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.pathService}/${id}/subscribe`);
  }

}
