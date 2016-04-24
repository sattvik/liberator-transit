# liberator-transit

liberator-transit is a Clojure library designed to add support to
[Liberator][liberator] for encoding sequences and maps into [Transit][transit].
It supports JSON, verbose JSON, and MessagePack encodings.

It is a fairly simple library, but it’s handy to have.

[![Build Status](https://travis-ci.org/sattvik/liberator-transit.svg?branch=master)][build-status]

  [liberator]: http://clojure-liberator.github.io/liberator/
  [transit]: https://github.com/cognitect/transit-format
  [build-status]: https://travis-ci.org/sattvik/liberator-transit (Travis CI build status)


## Installation

To install, just add the following to your project :dependencies:

```clojure
[io.clojure/liberator-transit "0.3.0"]
```


## Usage

### Loading liberator-transit

To load support for Transit into Liberator, just require the
`io.clojure.liberator-transit` namespace:

```clojure
(ns my.liberator.app
  (:require [io.clojure.liberator-transit]))
```


### Supporing Transit in a resource

The only other thing you need to do is to let Liberator know you would like to
support the Transit MIME types: `application/transit+json` and
`application/transit+msgpack`, as in:


```clojure
(defresource my-awesom-resource
  :available-media-types ["application/transit+json"
                          "application/transit+msgpack"
                          "application/json"]
  :handle-ok ["this" "is" "awesome"])
```

That’s it.  If your `handle-ok` returns a sequence or a map, it will now be
encoded into Transit/JSON or Transit/MessagePack if your client requests it.


### Getting verbose JSON

Both varieties of Transit/JSON use the same MIME type.  By default,
liberator-transit uses the non-verbose JSON format.  In order to get verbose
JSON output, a client needs to include "verbose" as part of the "Accept"
header.  For example:

```
curl -H "Accept: application/transit+json;verbose" \
    http://localhost:3000/hello
```

You can completely disable verbose JSON output by setting the
`:allow-json-verbose?` option to a false value.  Additionally, by setting
`"json-verbose-is-default?` to a true value, JSON responses will be verbose by
default.  See the next section about setting options for more information.

### Setting options

You can set various options to modify the behaviour of `liberator-transit`.
The supported options include:

* `:allow-json-verbose?`: if false, do not ever produce verbose JSON output.
* `:json-verbose-is-default?`: if true, produce verbose JSON output by default.
* `:initial-buffer-size`: The initial buffer size to use when generating the
  output.  Note that the buffer will automatically grow as needed.  It probably
  only makes sense to change this if you are serialising very large objects.
* `:handlers`: a map of write handlers that will be passed directly to transit

liberator-transit looks for its options in the Liberator context under they key
`:liberator-transit`.  As such you can set options anywhere in your resource
where you can modify the context.  For example, the following sample resource
will modify the context as part of the `exists?` decision:

```clojure
(defresource hello-resource [name]
  :exists? {:liberator-transit {:allow-json-verbose? false}}
  :available-media-types ["application/transit+json"]
  :handle-ok {:message (str "Hello, " name \!)})
```

Additionally, you can set options by specifying a `as-response` function.
transit-liberator provides `io.clojure.transit-liberator/as-response`,
available in two different arities.  The unary form simply takes an options map
and invokes Liberator’s default behaviour.  The following is equivalent to the
previous example:

```clojure
(defresource hello-resource [name]
  :available-media-types ["application/transit+json"]
  :handle-ok {:message (str "Hello, " name \!)}
  :as-response (transit-liberator/as-response
                 {:allow-json-verbose? false}))
```

Additionally, in the case you already have a custom `as-response` function you
would like to use, you can wrap it using transit-liberator’s `as-response`:

```clojure
(defresource hello-resource [name]
  :available-media-types ["application/transit+json"]
  :handle-ok {:message (str "Hello, " name \!)}
  :as-response (transit-liberator/as-response
                 {:allow-json-verbose? false}
                 my-as-response))
```

Note that any options specified elsewhere in your resource definition will
override any options set as part of the `as-response` call.

## Contributors

All contributors to liberator-transit by first commit:

* Daniel Solano Gómez
* Rafael Khan


## To-do

I am not sure what might be desired by user of the library, but a few ideas I
have include:

* Write a proper URI generator for the tests
* Write a Transit link generator for the tests
* Should liberator-transit modify the content type or add additional headers to
  let the consumer know it is producing verbose JSON?

## License

Copyright © 2014 Daniel Solano Gómez
All rights reserved.

The use and distribution terms for this software are covered by the Eclipse
Public License 1.0 <http://www.eclipse.org/legal/epl-v10.html> which can
be found in the file LICENSE at the root of this distribution.  By using this
software in any fashion, you are agreeing to be bound by the terms of this
license.  You must not remove this notice, or any other, from this software.
