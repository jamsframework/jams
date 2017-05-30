# jams-web-client

JAMS Web Client is a web front-end for JAMS Cloud Server.

## Development

JAMS Web Client is a single-page application (SPA). It is built with the JavaScript library [Vue.js](https://vuejs.org).

### Used libraries

* [vue](https://vuejs.org)
* [vue-resource](https://github.com/pagekit/vue-resource) for HTTP requests
* [vue-router](https://github.com/vuejs/vue-router) for routing
* [vuex](https://github.com/vuejs/vuex) for sharing application state across modules

### Installation

1. Install [npm](https://nodejs.org/en/download/).
2. Check out the [JAMSWebClient](http://svn.geogr.uni-jena.de/svn/jams/trunk/JAMSWebClient/) repository.
3. Change to the project directory.
4. Install all dependencies:

	```bash
	npm install
	```

### Running the development server

To start the development HTTP server and serve the website from `localhost:38081`:

```bash
npm run dev
```

### Building for production

To build the website for production:

```bash
npm run build
```

The result can be found in directory `./dist`.

Alternatively, you can use the custom build script that calls the above command, creates an archive and uploads it to the server:

```bash
./1-deploy.sh
```
