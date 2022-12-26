#Rowing player-team matching system

This project contains four microservices:

- authentication microservice
- user microservice
- request microservice
- activity microservice

#Running the microservices

Firstly, docker needs to be installed then run using docker compose up. It is used for sending notifications to users (to activity owners when new requests appear and to activity applicants when their request was responded to). Then, the four microservices can be started individually by running the Spring applications. Please open authentication microservice and user microservice before the request microservice, since the request microservice creates an admin account when it starts. A possible order in which the microservices can be started is: authentication, user, activity, request.

#Authentication microservice

The 'authentication-microservice' is responsible for registering new users and authenticating current ones. After successful authentication, this microservice will provide a JWT token which can be used to bypass the security on the other microservices. This token contains the memberID of the user that authenticated.

- To **register**, the users have to provide a unique **memberId** together with a **password**. Also, they need to provide personal details, such as name, gender, availability etc.

![image](Images/img.png)

- To **authenticate**, the user has to provide the **memberId** and the **password**. A token will be generated. It must be included in the further requests made by the user.

![image](Images/image7.png)

- A user also has the possibility of **changing** the account password.

![image](Images/image2.png)


#User microservice

All the actions a user can take are inside the 'user-microservice'. This microservice further makes requests to the other microservices when needed.

- Users can **create** an activity (both training and competition). They are remembered as the **owner** of the created activity.

![image](Images/image4.png)

![image](Images/image13.png)

- The owner of an activity (both training and competition) is allowed to **update** the activity.

![image](Images/image11.png)

![image](Images/image17.png)

- The owner of the activity (both training and competition) can **delete** the activity.

![image](Images/image16.png)

- Users are able to see all the activities (both training and competition) that they are able to join (they meet all the required criteria).

![image](Images/image14.png)

![image](Images/image18.png)

- A user is able to create a **request** to join an appropriate activity.

![image](Images/image1.png)

- Users can see all the **pending requests** for activities where they are owners.

![image](Images/image8.png)

- An activity owner can **accept** a request.

![image](Images/image5.png)

- An activity owner can **decline** a request.

![image](Images/image3.png)

- A user is able to **update** a specific existing availability.

![image](Images/image9.png)

- A user is able to **create** a new availability.

![image](Images/image6.png)

- A user is able to **delete** a specific existing availability.

![image](Images/image12.png)


#Request microservice

The 'request-microservice' is responsible for two things:

- managing the existing requests of users to join activities
- send notifications

_Notifications are sent using docker._

- Some existing endpoints (not directly called from the user microservice) include listing all the requests for which a specific user is the activity owner.

![image](Images/image10.png)


#Activity microservice

The 'activity-microservice' is responsible for creating, editing and deleting activities.
Activities include 'training' and 'competition'.  
For creating an activity, the user must be logged in and provide the required fields.
For 'trainings', these fields are:
- the owner ID (the ID of the user who wants to create the training)
- date of the training, in format "yyyy-MM-dd"
- boat type:
  -> "C4"
  -> "FOUR_PLUS" or
  -> "EIGTH_PLUS"
- start time of the training, in format "HH:MM"
- end time of the training, in format "HH:MM"
    - a list of the required positions for the training, as well as the number of each position type needed.
      The positions can be the following:
    - "COX"
    - "COACH"
    - "PORT_SIDE_ROWER"
    - "START_BOARD_SIDE_ROWER" and
    - "SCULLING_ROWER"

For 'competitions', the fields are the same as in the training with the following additions:
- allowed level:
    - "PROFESSIONAL" or
    - "AMATEUR"
- allowed gender:
    - "MALE"
    - "FEMALE" or
    - "OTHER"
- organization that the owner belongs to. (For example, "TU Delft")

All of these fields must be completed in order to create an activity, both in case of 'training' and 'competition'.

Once an activity is created, the user has the following possibilities:
- retrieve an activity from the database by providing the unique ID of the activity
    - If the ID does not correspond to any activity in the database, the system outputs a string informing the user that the ID could not be found.
- retrieve all the trainings in the database
- retrieve all the competitions in the database
- retrieve the competitions which are compatible with the user.
    - The compatibility is decided based on the user's experience level, gender, boat license,  available positions currently in the competition and user's availability.
- retrieve the trainings which are compatible with the user.
- filling a position in an activity.
    - For that, the user must provide the ID of the activity he wants to join, as well as the position he wants to fill. If the position is not available, or the ID of the activity is not valid, the user is informed of that.
- deleting an activity for the database. For that, the user must provide the ID of the activity and his unique ID.
    - If the user ID does not match the ID of the owner of the activity, the user is not allowed to delete the activity. If the activity does not exist, the user is informed.
- update training
    - The user must provide the fields that they wish to update inside an existing activity, as well as the ID of the user and the ID of the activity they wish to edit.
    - If the user ID does not correspond to the ID of the activity owner, the user does not have permission to edit the activity. If the activity does not exist, the user is informed of that.
- update competition: same as for training.


