export interface Post {
    id: number;
    title: string;
    content: string;
    date: string;
    author: string;
    topicId: number;
}

export interface CreatePostRequest {
    title: string;
    content: string;
    topicId: number;
}

export type PostOrder = 'asc' | 'desc';
