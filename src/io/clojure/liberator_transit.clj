; Copyright © 2014 Daniel Solano Gómez
; All rights reserved.
;
; The use and distribution terms for this software are covered by the Eclipse
; Public License 1.0 <http://www.eclipse.org/legal/epl-v10.html> which can be
; found in the file LICENSE at the root of this distribution.  By using this
; software in any fashion, you are agreeing to be bound by the terms of this
; license.  You must not remove this notice, or any other, from this software.

(ns io.clojure.liberator-transit
  "Main namespace for liberator-transit.  Just adds new methods to the
  multimethods that liberator uses to render maps and sequences."
  {:author "Daniel Solano Gómez"}
  (:require [liberator.representation :as liberator :refer [render-map-generic render-seq-generic]]
            [cognitect.transit :as transit])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def default-options
  "The default options for liberator-transit.

  * `:allow-json-verbose?`: if false, do not ever produce verbose JSON
    output
  * `:json-verbose-is-default?`: if true, produce verbose JSON output by
    default.
  * `:initial-buffer-size`: The initial buffer size to use when generating the
    output.  Note that the buffer will automatically grow as needed.  It
    probably only makes sense to change this if you are serialising very large
    objects."
  {:allow-json-verbose? true
   :json-verbose-is-default? false
   :initial-buffer-size 4096})

(defn ^:private requested-verbose?
  "Returns true if the givn Ring request contains an \"Accept\" header that
  contains the string \"verbose\"."
  [request]
  (some-> request
          (get-in [:headers "accept"])
          (.indexOf "verbose")
          (pos?)))

(defn ^:private json-type
  "Returns which JSON type should be produced by liberator-transit depending on
  the options passed in through the context and in the request headers.  The
  options are stored in the context map under the `:liberator-transit` key.
  The determination is done as follows:

  1. If `:allow-json-verbose?` option is set to a false value, return`:json`.
  2. If the request contains \"verbose\" as part of the \"Accept\" header, then
     return `:json-verbose`.
  3. If `:json-verbose-is-default?` option is set to a true value,
     return`:json-verbose`.
  4. If none of the above apply, return `:json`."
  [{:keys [liberator-transit request]}]
  (let [{:keys [allow-json-verbose?
                json-verbose-is-default?]} (merge default-options
                                                  liberator-transit)]
    (cond
      (not allow-json-verbose?) :json
      (requested-verbose? request) :json-verbose
      json-verbose-is-default? :json-verbose
      :default :json)))

;; If we return an input stream, Liberator will attempt to add a charset
;; parameter to the Content-Type header returned to the client.  For a transit
;; response, this is undesireable.  As a result, the TransitResponse type will
;; ensure that does not occur.
(defrecord TransitResponse [bytes]
  liberator.representation.Representation
  (as-response [_ context]
    {:body bytes
     :headers {"Content-Type" (get-in context [:representation :media-type])}}))

(defn ^:private render-as-transit
  "Renders the given `data` to an input stream.  Liberator will pass this
  stream to Ring, which will write the contents into the response.  `type`
  must be a supported transit-clj writer type, e.g. `:json`."
  [{options :liberator-transit} data type]
  (let [initial-size (get options :initial-buffer-size (:initial-buffer-size default-options))
        buffer (ByteArrayOutputStream. initial-size)
        writer (transit/writer buffer type (select-keys options [:handlers]))]
    (transit/write writer data)
    (->TransitResponse (ByteArrayInputStream. (.toByteArray buffer)))))

;; Renders a map using the JSON transit encoding.  If the original "Accept"
;; header included "verbose", i.e. `application/transit+json;verbose`, then
;; this will write verbose JSON output.  Otherwise, this will produce the
;; default JSON encoding.
(defmethod render-map-generic "application/transit+json"
  [data context]
  (render-as-transit context data (json-type context)))

;; Renders a map using the MessagePack transit encoding.
(defmethod render-map-generic "application/transit+msgpack"
  [data context]
  (render-as-transit context data :msgpack))

;; Renders a sequence using the JSON transit encoding.  If the original
;; "Accept" header included "verbose", i.e. `application/transit+json;verbose`,
;; then this will write verbose JSON output.  Otherwise, this will produce the
;; default JSON encoding.
(defmethod render-seq-generic "application/transit+json"
  [data context]
  (render-as-transit context data (json-type context)))

;; Renders a sequence using the MessagePack transit encoding.
(defmethod render-seq-generic "application/transit+msgpack"
  [data context]
  (render-as-transit context data :msgpack))

(defn as-response
  "This convenience function allows you to specify options to liberator-transit
  in one handy place.  As an alternative to specifying liberator-transit options
  elsewere in your resource, you can call this function as the value of
  `:as-response` key, as in:

      (defresource foo
        ; …
        :as-response (as-response {:allow-json-verbose? false})

  Note that if you do specify liberator-transit options elsewhere in your
  resource, they will override the options set here.

  Additionally, if you already have an `as response` function you would like to
  use, can wrap it with this one.  Otherwsie, this will just use Liberator’s
  default implementation."
  ([options]
   (as-response options liberator/as-response))
  ([options as-response-fn]
   (fn [data {context-options :liberator-transit :as context}]
     (as-response-fn data
                     (assoc context
                            :liberator-transit
                            (merge options context-options))))))
