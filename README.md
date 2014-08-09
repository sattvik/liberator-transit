# liberator-transit

liberator-transit is a Clojure library designed to add support to
[Liberator][liberator] for encoding sequences and maps into [Transit][transit].
It supports JSON, verbose JSON, and MessagePack encodings.

It is a fairly simple library, but it’s handy to have.

  [liberator]: http://clojure-liberator.github.io/liberator/
  [transit]: https://github.com/cognitect/transit-format

## Installation

To install, just add the following to your project :dependencies:

```clojure
[io.clojure.liberator-transit "0.1.0"]
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

## To-do

I am not sure what might be desired by user of the library, but a few ideas I
have include:

* Support configuration of liberator-transit via the Liberator context.  Possible cofiguration could include:
  * Disabling verbose JSON output
  * Making verbose JSON the default
  * Configuring the initial Transit buffer size
* Write a proper URI generator for the tests
* Write a Transit link generator for the tests

## License

Copyright © 2014 Daniel Solano Gómez
All rights reserved.

The use and distribution terms for this software are covered by the Eclipse
Public License 1.0 <http://www.eclipse.org/legal/epl-v10.html> which can
be found in the file LICENSE at the root of this distribution.  By using this
software in any fashion, you are agreeing to be bound by the terms of this
license.  You must not remove this notice, or any other, from this software.
