/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// const { onRequest } = require("firebase-functions/v2/https");
// const logger = require("firebase-functions/logger");


const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.onImageRated = functions.firestore
    .document("Images/{imageId}")
    .onUpdate((change, context) => {
      const newValue = change.after.data();
      const previousValue = change.before.data();

      // Check if the rating field has changed
      if (newValue.totalRatings !== previousValue.totalRatings) {
        const imageUrl = context.params.imageId;
        const userId = newValue.lastRatedBy;

        // Create a document in UserRatings collection
        return admin
            .firestore()
            .collection("UserRatings")
            .doc(userId + imageUrl)
            .set({
              rating: newValue.totalRatings,
              imageUrl: imageUrl,
              userId: userId,
            });
      }
      return null;
    });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
