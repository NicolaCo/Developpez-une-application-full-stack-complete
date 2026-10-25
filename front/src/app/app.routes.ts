import { Routes } from '@angular/router';
import { authGuard } from './core/guard/auth.guard';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { SignupComponent } from './pages/signup/signup.component';
import { TopicsComponent } from './pages/topics/topics.component';
import { PostsComponent } from './pages/posts/posts.component';
import { CreatepostComponent } from './pages/createpost/createpost.component';
import { PostDetailComponent } from './pages/post-detail/post-detail.component';
import { FeedComponent } from './pages/feed/feed.component';

export const routes: Routes = [
    {
        path: '',
        component: HomeComponent
    },
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: 'signup',
        component: SignupComponent
    },
    {
        path: 'topics',
        component: TopicsComponent,
        canActivate: [authGuard]
    },
    {
        path: 'feed',
        component: FeedComponent,
        canActivate: [authGuard]
    },
    {
        path: 'posts',
        component: PostsComponent,
        canActivate: [authGuard]
    },
    {
        path: 'posts/new',
        component: CreatepostComponent,
        canActivate: [authGuard]
    },
    {
        path: 'posts/:id',
        component: PostDetailComponent,
        canActivate: [authGuard]
    },
];
