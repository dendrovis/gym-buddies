import * as functions from 'firebase-functions'
import * as admin from 'firebase-admin'
admin.initializeApp()

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//

const db = admin.firestore() // Firestore DB instance

console.log(`Firebase Function Version: 141020190255`)
console.log(`Dep Versions listed below`);
console.log(process.versions)


export const calcuateGymRatings = functions.firestore
    .document('gymreviews/{gymId}/users/{userId}')
    .onWrite(async(change, context) => {
        const document = change.after.exists ? change.after.data() : null;
        // Get an object with the previous document value (for update or delete)
        const oldDocument = change.before.data();
        console.log(`Processing Gym Rating update for Gym ${context.params.gymId} due to change by User ${context.params.userId}`)
        let rating = Number(0.0)
        let status = 0 // 0 - create, 1 - update, 2 - delete
        if (document === null && oldDocument) {
            console.log(`User ${context.params.userId} deleted review for Gym ${context.params.gymId}`)
            rating = oldDocument.rating // Rating to subtract
            rating *= -1 // Inverse cause delete
            console.log(`Rating Difference: ${rating}`)
            status = 2
        } else if (document) {
            rating = parseFloat(document.rating) // Get current rating
            const oldRating = oldDocument ? oldDocument.rating : Number(0.0) // Check if there is old rating, if so its update (add the difference of new and old ratings)
            status = oldDocument ? 1 : 0
            console.log(`User ${context.params.userId} ${oldDocument ? "updated" : "added" } review for Gym ${context.params.gymId}`)
            rating = rating - oldRating // Can be positive or negative
            console.log(`Rating Difference: ${rating >= 0 ? "+" : ""}${rating}`)
        } else {
            console.log("An error occurred processing gym ratings (cannot find object data is null)")
            return true
        }

        // Get the gym object itself and update the values (rating count)
        console.log(`Updating Gym Object...`)
        await db.doc(`gymreviews/${context.params.gymId}`).get().then(async documentSnapshot => {
            let gymRating = documentSnapshot.exists ? documentSnapshot.data() : null
            if (gymRating === null || typeof gymRating === 'undefined') {
                console.log(`Gym ${context.params.gymId} does not exist, creating data`)
                gymRating = {} as FirebaseFirestore.DocumentData
            }
            // Null Checks
            if (typeof gymRating.totalRating === 'undefined') gymRating.totalRating = parseFloat("0.0")
            if (typeof gymRating.count === 'undefined') gymRating.count = parseFloat("0")
            if (typeof gymRating.averageRating === 'undefined') gymRating.averageRating = parseFloat("0.0")

            gymRating.totalRating += rating
            gymRating.count = status === 0 ? gymRating.count + 1 : status === 2 ? gymRating.count - 1 : gymRating.count
            gymRating.averageRating = (gymRating.count === 0) ? parseFloat("0") : gymRating.totalRating / gymRating.count
            console.log(`Gym ${context.params.gymId} latest data: average: ${gymRating.averageRating}, total: ${gymRating.totalRating}, count: ${gymRating.count}`)
            await db.doc(`gymreviews/${context.params.gymId}`).set(gymRating).then(res => {
                console.log(`Successfully updated gym Object ${context.params.gymId} at ${res.writeTime.toDate()}`)
            }).catch(err => {
                console.log(`Failed to update database: ${err}`)
            })
        }).catch(err => {
            console.log(`Failed to retrieve gym object: ${err}`)
        }) 
        return true
    }) 