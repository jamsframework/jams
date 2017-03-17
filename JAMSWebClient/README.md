# jams-web-client

JAMS Web Client is a web front-end for JAMS Cloud Server.

## Development

JAMS Web Client is a single-page application (SPA). It is built with the JavaScript framework [Vue.js](https://vuejs.org).

### Used components

* [vue](https://vuejs.org)
* [vue-resource](https://github.com/pagekit/vue-resource) for HTTP requests
* [vue-router](https://github.com/vuejs/vue-router) for routing
* [vuex](https://github.com/vuejs/vuex) to share application state across modules

### Installation

Check out the repository, then change to the project directory.

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

Alternatively, you can use the custom build script that calls the above command and creates an archive ready for upload:

```bash
./1-deploy.sh
```
