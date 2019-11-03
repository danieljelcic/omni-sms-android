from flask import Flask, request
from twilio.twiml.messaging_response import MessagingResponse
from twilio.rest import Client
from pymongo import MongoClient
from urllib.parse import quote

app = Flask(__name__)
account_sid = 'AC4f7b4dedf3f45eb446c208f8ae506b25'
auth_token = '50b93029678586704718f2b5ec59dd79'
tw_client = Client(account_sid, auth_token)


mdb_client = MongoClient("mongodb+srv://djelci01:{0}@omnisms-1l7x0.mongodb.net/test?retryWrites=true&w=majority".format(quote("DwNX3nHNbb@Kh@N")))
db = mdb_client.sessions

@app.route("/hello")
def hello_world():
    return "Hello world!"


@app.route("/sms", methods=['GET', 'POST'])
def reply_to_sms():

    print(request.values.get('Body', None))
    print("request.values:", request.values)

    body = request.values.get('Body', None)
    from_num = request.values.get('From', None)

    to_insert = {
            "body" : body,
            "from" : from_num
        }

    if body.split(' ')[0] != "omniSMS":

        db.other_test.insert_one(to_insert)
        print("Inserted in other")

    else:

        db.sessions_test.insert_one(to_insert)
        print("Inserted in sessions")

    resp = MessagingResponse()
    resp.message("Thank you for saying \"{0}\"".format(request.values.get('Body', None)))

    return str(resp)


@app.route("/send_sms", methods=["GET"])
def send_sms_from_url():

    number = "+" 
    number += request.args.get('num', None)
    message_body = request.args.get('body', None)

    message = tw_client.messages.create(
                     body=message_body,
                     from_="+12568184448",
                     to=number
                    )

    return message.sid





if __name__ == "__main__":
    app.run(debug=True)
