/**
 * @fileoverview
 * Provides methods for the Hello Endpoints sample UI and interaction with the
 * Hello Endpoints API.
 */

var google = google || {};
google.appengine = google.appengine || {};
google.appengine.sportconnector = google.appengine.sportconnector || {};
google.appengine.sportconnector.confirmAccount = google.appengine.sportconnector.confirmAccount || {};
google.appengine.sportconnector.confirmEmail = google.appengine.sportconnector.confirmEmail || {};
google.appengine.sportconnector.resetPass = google.appengine.sportconnector.resetPass || {};


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
 * Shows a message for user.
 */
google.appengine.sportconnector.confirmAccount.print = function(msg) {
  var element = document.getElementById("msg");
  element.innerHTML = msg;
  element = document.getElementById("wait");
    element.hidden = true;
  element = document.getElementById("intent");
  element.hidden = false;
};

/**
 * Execute confirmation via the API.
 * @param {string} id ID of the greeting.
 */
google.appengine.sportconnector.confirmAccount.execute = function() {
  var params = getParams();
  if(typeof params['id'] === "string" && typeof params['x'] === "string")
    gapi.client.sportConnectorApi.confirmAccount({'id':params['id'],'x':params['x']}).execute(
        function(resp) {
          if (!resp.code)
            google.appengine.sportconnector.confirmAccount.print('Ваша учетная запись '+decodeURIComponent(params['id'])+' успешно активирована!');
          else
            google.appengine.sportconnector.confirmAccount.print(resp.message);
        });
  else
    google.appengine.sportconnector.confirmAccount.print("Ошибка! Ваша учетная запись не активирована!");
};



/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
google.appengine.sportconnector.confirmAccount.init = function(apiRoot) {
  // Loads the OAuth and helloworld APIs asynchronously, and triggers login
  // when they have completed.
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
        google.appengine.sportconnector.confirmAccount.execute();
    }
  }

  apisToLoad = 1; // must match number of calls to gapi.client.load()
  gapi.client.load('sportConnectorApi', 'v1', callback, apiRoot);
};


/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
google.appengine.sportconnector.confirmEmail.init = function(apiRoot) {
  // Loads the OAuth and helloworld APIs asynchronously, and triggers login
  // when they have completed.
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
        google.appengine.sportconnector.confirmEmail.execute();
    }
  }

  apisToLoad = 1; // must match number of calls to gapi.client.load()
  gapi.client.load('sportConnectorApi', 'v1', callback, apiRoot);
};


/**
 * Execute confirmation via the API.
 * @param {string} id ID of the greeting.
 */
google.appengine.sportconnector.confirmEmail.execute = function() {
  var params = getParams();
  if(typeof params['id'] === "string" && typeof params['x'] === "string")
    gapi.client.sportConnectorApi.confirmEmail({'id':params['id'],'x':params['x']}).execute(
        function(resp) {
          if (!resp.code)
            google.appengine.sportconnector.confirmAccount.print('Ваша почта изменена!');
          else
            google.appengine.sportconnector.confirmAccount.print(resp.message);
        });
  else
    google.appengine.sportconnector.confirmAccount.print("Ошибка! Ваша почта не подтверждена!");
};

google.appengine.sportconnector.confirmEmail.print = google.appengine.sportconnector.confirmAccount.print;

/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
google.appengine.sportconnector.resetPass.init = function(apiRoot) {
  // Loads the OAuth and helloworld APIs asynchronously, and triggers login
  // when they have completed.
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
        google.appengine.sportconnector.resetPass.enableButton();
    }
  }

  apisToLoad = 1; // must match number of calls to gapi.client.load()
  gapi.client.load('sportConnectorApi', 'v1', callback, apiRoot);
};


/**
 * Execute confirmation via the API.
 * @param {string} id ID of the greeting.
 */
google.appengine.sportconnector.resetPass.execute = function() {
  var params = getParams();
  if(typeof params['id'] === "string" && typeof params['x'] === "string")
    gapi.client.sportConnectorApi.resetPass({'id':params['id'],'x':params['x']}).execute(
        function(resp) {
          if (!resp.code)
            google.appengine.sportconnector.resetPass.print('Ваша почта изменена!');
          else
            google.appengine.sportconnector.resetPass.print(resp.message);
        });
  else
    google.appengine.sportconnector.resetPass.print("Ошибка! Ваша почта не подтверждена!");
};

/**
 * Enables the button callbacks in the UI.
 */
google.appengine.sportconnector.resetPass.enableButton = function() {
  var getGreeting = document.querySelector('#resetPass');
  getGreeting.addEventListener('click', function(e) {
    google.appengine.sportconnector.resetPass.execute(
        document.querySelector('#pass').value, document.querySelector('#confirmPass').value);
  });

};

google.appengine.sportconnector.resetPass.print = google.appengine.sportconnector.confirmAccount.print;