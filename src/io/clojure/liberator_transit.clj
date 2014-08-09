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

(defn ^:private render-as-transit
  "Renders the given `data` to an input stream.  Liberator will pass this
  stream to Ring, which will write the contents into the response.  `type`
  must be a supported transit-clj writer type, e.g. `:json`."
  [data type]
  (let [buffer (ByteArrayOutputStream. 4096)        
        writer (transit/writer buffer type)]        
    (transit/write writer data)                     
    (ByteArrayInputStream. (.toByteArray buffer))))

;; Renders a map using the JSON transit encoding.  If the original "Accept"
;; header included "verbose", i.e. `application/transit+json;verbose`, then
;; this will write verbose JSON output.  Otherwise, this will produce the
;; default JSON encoding.
(defmethod render-map-generic "application/transit+json"
  [data context]
  (let [accept-header (get-in context [:request :headers "accept"])]
    (if (pos? (.indexOf accept-header "verbose"))
      (render-as-transit data :json-verbose)  
      (render-as-transit data :json))))

;; Renders a map using the MessagePack transit encoding.
(defmethod render-map-generic "application/transit+msgpack"
  [data _]
  (render-as-transit data :msgpack))

;; Renders a sequence using the JSON transit encoding.  If the original
;; "Accept" header included "verbose", i.e. `application/transit+json;verbose`,
;; then this will write verbose JSON output.  Otherwise, this will produce the
;; default JSON encoding.
(defmethod render-seq-generic "application/transit+json"
  [data context]
  (let [accept-header (get-in context [:request :headers "accept"])]
    (if (pos? (.indexOf accept-header "verbose"))
      (render-as-transit data :json-verbose)  
      (render-as-transit data :json))))

;; Renders a sequence using the MessagePack transit encoding.
(defmethod render-seq-generic "application/transit+msgpack"
  [data _]
  (render-as-transit data :msgpack))
