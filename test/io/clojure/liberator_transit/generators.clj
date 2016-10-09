; Copyright © 2014 Daniel Solano Gómez
; All rights reserved.
;
; The use and distribution terms for this software are covered by the Eclipse
; Public License 1.0 <http://www.eclipse.org/legal/epl-v10.html> which can be
; found in the file LICENSE at the root of this distribution.  By using this
; software in any fashion, you are agreeing to be bound by the terms of this
; license.  You must not remove this notice, or any other, from this software.

(ns io.clojure.liberator-transit.generators
  (:require [clojure.string :as str]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.generators :as gen'])
  (:import [java.util Date]))

(def nil-generator
  (gen/return nil))

(def float-generator
  (gen/fmap (fn [d]
              (cond
                (.isNaN d)
                Float/NaN

                (= Double/POSITIVE_INFINITY d)
                Float/POSITIVE_INFINITY

                (= Double/NEGATIVE_INFINITY d)
                Float/NEGATIVE_INFINITY

                :default
                (unchecked-float d)))
            gen/double))

(def big-decimal-generator
  (gen/fmap
    #(BigDecimal. %)
    (gen'/string-from-regex #"\d+\.\d+")))

(def big-int-generator
  (gen/fmap
    #(BigInteger. %)
    (gen'/string-from-regex #"\d+")))

(def time-generator
  (gen/fmap
    (fn [offset]
      (Date. (+ (System/currentTimeMillis) offset)))
    gen/int))

; TODO: really generate URIs
(def uri-generator
  (gen/elements [(java.net.URI. "/foo/bar/baz")
                 (java.net.URI. "http://example.com")
                 (java.net.URI. "https://user:foo@example.com/baz/dldl/lsjdlj?q=42")
                 (java.net.URI. "https://user:foo@example.com/baz/dldl/lsjdlj?q=42#ldjd")
                 (java.net.URI. "urn:bar")
                 (java.net.URI. "file:foo")
                 (java.net.URI. "file:/foo")
                 (java.net.URI. "http://localhost:9000/check")
                 (java.net.URI. "http://[::1]:9000/check")]))

(def simple-type-generator
  (gen/frequency [[10 gen/simple-type]
                  [1 nil-generator]
                  [1 gen/double]
                  [1 float-generator]
                  [1 big-decimal-generator]
                  [1 big-int-generator]
                  [1 time-generator]
                  [1 gen/uuid]
                  [1 uri-generator]]))

(defn container-generator
  [inner-type]
  (gen/one-of [(gen/vector inner-type)
               (gen/list inner-type)
               (gen/map inner-type inner-type)
               (gen/set inner-type)]))

(def content-generator
  (gen/recursive-gen container-generator simple-type-generator))

(def sequence-generator
  (gen/one-of [(gen/vector content-generator)
               (gen/list content-generator)]))

(def map-generator
  (gen/map simple-type-generator content-generator))
