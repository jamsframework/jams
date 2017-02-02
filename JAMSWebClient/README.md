# jams-web-client

JAMS Web Client is a web front-end for JAMS Cloud Server.

## Development

JAMS Web Client is a single-page application (SPA). It is built with the JavaScript framework Vue.js.

### Documentation of used components

* [vue](https://vuejs.org)
* [vue-cookie](https://github.com/alfhen/vue-cookie) for handling cookies
* [vue-resource](https://github.com/pagekit/vue-resource) for HTTP requests
* [vue-router](https://github.com/vuejs/vue-router) for handling routing
* [vuex](https://github.com/vuejs/vuex) for handling application state
* [vuex-router-sync](https://github.com/vuejs/vuex-router-sync) for syncing routes and application state

### Installation

To install all dependencies:

```bash
npm install
```

### Running the development server

To serve with hot reload at `localhost:38081`:

```bash
npm run dev
```

### Running tests

To run tests:

```bash
# Run unit tests
npm run unit

# Run e2e tests
npm run e2e

# Run all tests
npm test
```

### Building for production

To build for production with minification:

```bash
npm run build
```

For detailed explanation on how things work, checkout the [guide](http://vuejs-templates.github.io/webpack/) and [docs for vue-loader](http://vuejs.github.io/vue-loader).
