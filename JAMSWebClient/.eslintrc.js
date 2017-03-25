module.exports = {
	// https://github.com/feross/standard/blob/master/RULES.md#javascript-standard-style
	extends: "standard",

	parser: "babel-eslint",

	parserOptions: {
		sourceType: "module"
	},

	// Required to lint *.vue files
	plugins: [
		"html"
	],

	root: true,

	"rules": {
		"arrow-parens": ["error", "always"],
		"generator-star-spacing": 0,
		"indent": ["error", "tab", {"SwitchCase": 1}],
		"no-debugger": process.env.NODE_ENV === "production" ? "error" : 0,
		"no-tabs": 0,
		"operator-linebreak": ["error", "before"],
		"quotes": ["error", "double"],
		"semi": ["error", "always"],
		"space-before-function-paren": ["error", "never"]
	}
};
