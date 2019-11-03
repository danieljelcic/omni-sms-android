from twilio.rest import Client

# DANGER! This is insecure. See http://twil.io/secure
account_sid = 'AC4f7b4dedf3f45eb446c208f8ae506b25'
auth_token = '50b93029678586704718f2b5ec59dd79'
client = Client(account_sid, auth_token)

message = client.messages.create(
                     body="Join Earth's mightiest heroes. Like Kevin Bacon.",
                     from_='+12568184448',
                     to='+17815393033'
                 )

print(message.sid)