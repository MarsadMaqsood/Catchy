//Youtube Channel Link
//https://www.youtube.com/channel/UCGZF_fq2lEDAPl_BkDSphwQ

//Catchy Series
//https://youtube.com/playlist?list=PLFzlb57tNKUOrFIcicZ88qmaeHTA_9t_6

//Website Link
//https://marsad.dev


const functions = require("firebase-functions");
const admin = require('firebase-admin');


admin.initializeApp(functions.config().firestore);

exports.sheduleFuntionExpiredStories = functions.pubsub
    .schedule('every 30 minutes').onRun((context) => {

        //current time millis
        const timeMillis = admin.firestore.Timestamp.now().toMillis();


        admin.firestore().collection('Stories').where('expiryData', '<=', timeMillis).get()
            .then((snapshot) => {

                snapshot.docs.forEach((data) => {

                    console.log("Name: " + data.get('name'));
                    console.log("id: " + data.get('id'));
                    console.log("uid: " + data.get('uid'));

                    //delete expired document

                    admin.firestore().collection('Stories').doc(data.id).delete();

                });


            }).catch((error) => {
                console.log("Error: " + error);
            });

    });


exports.onStoryUpload = functions.firestore.document('Stories/{storyID}')
    .onCreate((snapshot, context) => {

        //return all the data inside document
        var data = snapshot.data();

        const timeMillis = admin.firestore.Timestamp.now().toMillis();

        const expiryDate = timeMillis + (24 * 60 * 60 * 1000);

        admin.firestore().collection('Stories').doc(data.id).update({

            "expiryData": expiryDate,

        });

    });