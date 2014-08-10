; Copyright © 2014 Daniel Solano Gómez
; All rights reserved.
;
; The use and distribution terms for this software are covered by the Eclipse
; Public License 1.0 <http://www.eclipse.org/legal/epl-v10.html> which can be
; found in the file LICENSE at the root of this distribution.  By using this
; software in any fashion, you are agreeing to be bound by the terms of this
; license.  You must not remove this notice, or any other, from this software.

(ns io.clojure.liberator-transit.generators
  (:require [clojure.data.generators :as data-gen]
            [clojure.test.check.generators :as gen]))

(def nil-generator
  (gen/return nil))

(def normal-float-generator
  (gen/make-gen
    (fn [^java.util.Random rnd size]
      (loop []
        (let [value (Float/intBitsToFloat (.nextInt rnd))]
          (if (or (.isNaN value) (.isInfinite value))
            (recur)
            [value []]))))))

(def float-generator
  (gen/frequency [[17 normal-float-generator]
                  [1 (gen/return Float/NEGATIVE_INFINITY)]
                  [1 (gen/return Float/POSITIVE_INFINITY)]
                  [1 (gen/return Float/NaN)]]))

(def normal-double-generator
  (gen/make-gen
    (fn [^java.util.Random rnd _]
      (loop []
        (let [value (Double/longBitsToDouble (.nextLong rnd))]
          (if (or (.isNaN value) (.isInfinite value))
            (recur)
            [value []]))))))

(def double-generator
  (gen/frequency [[17 normal-double-generator]
                  [1 (gen/return Double/NEGATIVE_INFINITY)]
                  [1 (gen/return Double/POSITIVE_INFINITY)]
                  [1 (gen/return Double/NaN)]]))

(def big-decimal-generator
  (gen/make-gen
    (fn [^java.util.Random rnd size]
      (binding [data-gen/*rnd* rnd]
        [(data-gen/bigdec) []]))))

(def big-int-generator
  (gen/make-gen
    (fn [^java.util.Random rnd size]
      (binding [data-gen/*rnd* rnd]
        [(data-gen/bigint) []]))))

(def time-generator
  (gen/make-gen
    (fn [^java.util.Random rnd size]
      (binding [data-gen/*rnd* rnd]
        [(data-gen/date) []]))))

(def uuid-generator
  (gen/make-gen
    (fn [^java.util.Random rnd size]
      (binding [data-gen/*rnd* rnd]
        [(data-gen/uuid) []]))))

(def uri-scheme-generator
  (gen/one-of [nil-generator (gen/fmap clojure.string/join (gen/not-empty (gen/vector gen/char-alpha)))]))

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
                  [1 double-generator]
                  [1 float-generator]
                  [1 big-decimal-generator]
                  [1 big-int-generator]
                  [1 time-generator]
                  [1 uuid-generator]
                  [1 uri-generator]]))

(defn set-generator
  [inner-type]
  (gen/fmap set (gen/vector inner-type)))

(defn container-generator
  [inner-type]
  (gen/one-of [(gen/vector inner-type)
               (gen/list inner-type)
               (gen/map inner-type inner-type)
               (set-generator inner-type)]))

(def content-generator
  (gen/recursive-gen container-generator simple-type-generator))

(def sequence-generator
  (gen/one-of [(gen/vector content-generator)
               (gen/list content-generator)]))

(def map-generator
  (gen/map simple-type-generator content-generator))
