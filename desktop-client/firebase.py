import pyrebase


class FirebaseManager:

    config = {
        'apiKey': "AIzaSyAdSb0s_pVMyfwVNp9jBicMAEwsfTlHtLk",
        'authDomain': "dore-tours-vandy.firebaseapp.com",
        'databaseURL': "https://dore-tours-vandy.firebaseio.com",
        'projectId': "dore-tours-vandy",
        'storageBucket': "dore-tours-vandy.appspot.com",
        'messagingSenderId': "1039373822265"
    };

    def __init__(self):
        self.firebase = pyrebase.initialize_app(self.config)

    def fetch(self, keychain):
        return self._get_keychain_end(keychain).get().val()

    def set(self, keychain, val):
        self._get_keychain_end(keychain).set(val)

    def _get_keychain_end(self, keychain):
        end = self.firebase.database()
        for key in keychain:
            end = end.child(key)
        return end
