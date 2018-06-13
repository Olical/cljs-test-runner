# cljs-test-runner changes

## 2.0.0

 * `-e` became `-x` as a shortcut for `--env`.
 * Added `--namespace` (`-n`) so you can test a single namespace by it's symbol.
 * Added `--namespace-regex` (`-r`) so you can test namespaces matching a regex. This default to any namespace ending in `-test`.

## 1.0.0

 * `--watch` support thanks to [@eval](https://github.com/eval) in [#2](https://github.com/Olical/cljs-test-runner/pull/2).

## 0.1.1

 * Print errors that originate from the ClojureScript compiler or doo. Things like missing namespaces were failing silently.

## 0.1.0

 * Initial release.
