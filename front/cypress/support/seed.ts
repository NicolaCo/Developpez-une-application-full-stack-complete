export const E2E_USERS = {
  subscriber: {
    email: 'e2e@mdd.test',
    username: 'e2e_user',
    password: 'E2ePassword1!'
  },
  author: {
    email: 'e2e.author@mdd.test',
    username: 'e2e_author',
    password: 'E2ePassword1!'
  }
} as const;

export const E2E_TOPICS = {
  subscribed: ['Angular', 'Spring Boot'],
  notSubscribed: 'Tests'
} as const;

export const E2E_POSTS = {
  withComment: 'Découvrir Angular',
  unsubscribedTopicPost: 'Tester son application'
} as const;
