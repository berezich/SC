/**
 * @fileoverview
 * Provides methods for the Hello Endpoints sample UI and interaction with the
 * Hello Endpoints API.
 */

/** google global namespace for Google projects. */
var google = google || {};

/** appengine namespace for Google Developer Relations projects. */
google.appengine = google.appengine || {};

/** samples namespace for App Engine sample code. */
google.appengine.sportconnector = google.appengine.sportconnector || {};

/** hello namespace for this sample. */
google.appengine.sportconnector.confirm = google.appengine.sportconnector.confirm || {};


function getParams () {
  // This function is anonymous, is executed immediately and
  // the return value is assigned to QueryString!
  var query_string = {};
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for (var i=0;i<vars.length;i++) {
    var pair = vars[i].split("=");
        // If first entry with this name
    if (typeof query_string[pair[0]] === "undefined") {
      query_string[pair[0]] = pair[1];
        // If second entry with this name
    } else if (typeof query_string[pair[0]] === "string") {
      var arr = [ query_string[pair[0]],pair[1] ];
      query_string[pair[0]] = arr;
        // If third or later entry with this name
    } else {
      query_string[pair[0]].push(pair[1]);
    }
  }
    return query_string;
};

/**
 * Prints a greeting to the greeting log.
 * param {Object} greeting Greeting to print.
 */
google.appengine.sportconnector.confirm.print = function(msg) {
  var element = document.createElement('div');
  element.classList.add('row');
  element.innerHTML = msg;
  document.querySelector('#outputLog').appendChild(element);
};

/**
 * Execute confirmation via the API.
 * @param {string} id ID of the greeting.
 */
google.appengine.sportconnector.confirm.execute = function() {
  var params = getParams();
  if(typeof params['id'] === "string" && typeof params['x'] === "string")
    gapi.client.sportConnectorApi.confirmAccount({'id':params['id'],'x':params['x']}).execute(
        function(resp) {
          if (!resp.code)
            google.appengine.sportconnector.confirm.print('Ваша учетная запись '+decodeURIComponent(params['id'])+' успешно активирована!');
          else
            google.appengine.sportconnector.confirm.print(resp.message);
        });
  else
    google.appengine.sportconnector.confirm.print("Ошибка! Ваша учетная запись не активирована!");
};



/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
google.appengine.sportconnector.confirm.init = function(apiRoot) {
  // Loads the OAuth and helloworld APIs asynchronously, and triggers login
  // when they have completed.
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
        google.appengine.sportconnector.confirm.execute();
    }
  }

  apisToLoad = 1; // must match number of calls to gapi.client.load()
  gapi.client.load('sportConnectorApi', 'v1', callback, apiRoot);
};
