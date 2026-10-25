export interface Comment {
    id: number;
    content: string;
    date: string;
    author: string;
    postId: number;
}

export interface CreateCommentRequest {
    content: string;
}
