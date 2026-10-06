# P6-Full-Stack-reseau-dev

## Front

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 14.1.3 and has since been upgraded to **Angular 22.2.1**.

### Requirements

- **Node.js >= 22.22.3** (enforced by `engines`). The version to use is pinned in `front/.nvmrc`:

```bash
cd front
nvm use
npm install
```

Skipping `nvm use` will make `npm install` fail on the `engines` check.

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

### Build

Run `ng build` to build the project. The build artifacts will be stored in `dist/front/browser/` (the `application` builder emits a `browser` subdirectory).

### Tests

Run `npm test` for the unit tests. They run on **Vitest** in a jsdom environment — no browser and no Chrome installation needed:

```bash
npm test              # single run
npm test -- --watch   # watch mode
```

Coverage is available with `npm test -- --coverage`.

### Where to start

As you may have seen if you already started the app, a simple home page containing a logo, a title and a button is available. If you take a look at its code (in the `home.component.html`) you will see that an external UI library is already configured in the project.

This library is `@angular/material`, one of the most famous in the angular ecosystem. As you can see on their docs (https://material.angular.dev/), it contains a lot of highly customizable components that will help you design your interfaces quickly.

Note: I recommend to use material however it's not mandatory, if you prefer you can get rid of it.

Good luck!
