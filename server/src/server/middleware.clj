(ns server.middleware)

(defn wrap-content-type
  "Sets the Content-Type header of a response to be that of the given content-type"
  [handler content-type]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))