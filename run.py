from flask import Flask, request
from twilio.twiml.messaging_response import MessagingResponse
from twilio.rest import Client
from pymongo import MongoClient
from urllib.parse import quote
import os

app = Flask(__name__)
account_sid = os.environ['TWILIO_ACCOUNT_SID']
print("account_sid:", account_sid)
auth_token = os.environ['TWILIO_AUTH_TOKEN']
print("auth_token:", auth_token)
tw_client = Client(account_sid, auth_token)

mdb_pw = os.environ['MONGODB_ATLAS_PW']
print("mdb_pw:", mdb_pw)
mdb_uri = "mongodb+srv://djelci01:{0}@omnisms-1l7x0.mongodb.net/test?retryWrites=true&w=majority".format(quote(mdb_pw))
print("mdb_uri:", mdb_uri)
mdb_client = MongoClient(mdb_uri)
db = mdb_client.sessions


@app.route("/hello")
def hello_world():
    return "Hello world!"


@app.route("/sms", methods=['GET', 'POST'])
def reply_to_sms():

    '''
    Request format:

    CombinedMultiDict([ImmutableMultiDict([]), ImmutableMultiDict([
        ('ToCountry', 'US'), 
        ('ToState', 'AL'), 
        ('SmsMessageSid', 'SM6da2dd63789a40b907d283e05c6e117f'), 
        ('NumMedia', '0'), 
        ('ToCity', 'BRENTWOOD'), 
        ('FromZip', '02155'), 
        ('SmsSid', 'SM6da2dd63789a40b907d283e05c6e117f'), 
        ('FromState', 'MA'), 
        ('SmsStatus', 'received'), 
        ('FromCity', 'MEDFORD'), 
        ('Body', 'omniSMS Message 14'), 
        ('FromCountry', 'US'), 
        ('To', '+12568184448'), 
        ('ToZip', '37027'), 
        ('NumSegments', '1'), 
        ('MessageSid', 'SM6da2dd63789a40b907d283e05c6e117f'), 
        ('AccountSid', 'AC4f7b4dedf3f45eb446c208f8ae506b25'), 
        ('From', '+17815393033'), 
        ('ApiVersion', '2010-04-01')])])
    
    '''


    body = request.values.get('Body', None)
    from_num = request.values.get('From', None)

    to_insert = {
            "body" : body,
            "from" : from_num
        }


    # distinguishing between omniSMSs and others
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
