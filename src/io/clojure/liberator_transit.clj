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
  (:require [liberator.representation :refer [render-map-generic render-seq-generic]]
            [cognitect.transit :as transit])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def default-options
  "The default options for liberator-transit.

  * `:support-json-verbose?`: if false, do not ever produce verrbose JSON
    output
  * `:json-verbose-is-default?`: if true, produce verbose JSON output by
    default.
  * `:initial-buffer-size`: The initial buffer size to use when generating the
    output.  Note that the buffer will automatically grow as needed.  It
    probably only makes sense to change this if you are serialising very large
    objects."
  {:support-json-verbose? true
   :json-verbose-is-default? false
   :initial-buffer-size 4096})

(defn ^:private requested-verbose?
  "Returns true if the givn Ring request contains an \"Accept\" header that
  contains the string \"verbose\"."
  [request]
  (-> request
      (get-in [:headers "accept"])
      (.indexOf "verbose")
      (pos?)))

(defn ^:private json-type
  "Returns which JSON type should be produced by liberator-transit depending on
  the options passed in through the context and in the request headers.  The
  options are stored in the context map under the `:liberator-transit` key.
  The determination is done as follows:

  1. If `:support-json-verbose?` option is set to a false value, return`:json`.
  2. If the request contains \"verbose\" as part of the \"Accept\" header, then
     return `:json-verbose`.
  3. If `:json-verbose-is-default?` option is set to a true value,
     return`:json-verbose`.
  4. If none of the above apply, return `:json`."
  [{:keys [liberator-transit request]}]
  (let [{:keys [support-json-verbose?
                json-verbose-is-default?]} (merge default-options
                                                  liberator-transit)]
    (cond
      (not support-json-verbose?) :json
      (requested-verbose? request) :json-verbose
      json-verbose-is-default? :json-verbose
      :default :json)))

(defn ^:private render-as-transit
  "Renders the given `data` to an input stream.  Liberator will pass this
  stream to Ring, which will write the contents into the response.  `type`
  must be a supported transit-clj writer type, e.g. `:json`."
  [context data type]
  (let [initial-size (get-in context
                             [:liberator-transit
                              :initial-buffer-size]
                             (:initial-buffer-size default-options))
        buffer (ByteArrayOutputStream. initial-size)
        writer (transit/writer buffer type)]
    (transit/write writer data)
    (ByteArrayInputStream. (.toByteArray buffer))))

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
