The List Organisations With No Abuse Contact README:


How to run:
java -jar listorgswithnoabusec.jar --passwd mqTwvGYzGeKVDzjDFeR2   (or, if you prefer java -jar /home/dbase/csapps/listorgswithnoabusec.jar --passwd mqTwvGYzGeKVDzjDFeR2)
This will print the resulting list. If you want it in a file, run
java -jar listorgswithnoabusec.jar --passwd mqTwvGYzGeKVDzjDFeR2 > filename

Where is it?
/home/dbase/csapps on puppy

How long does it take to run?
Approx. 15 min

How many are there?
Ballpark figure: somewhere in the region of 16400

The result explained:
You'll get a list of organisations and their contacts on format
<organisation>|<email>|<sponsoring lir email>
The <organisation> is the organisation without an abuse contact.
The <email> is the first e-mail of <organisation> as stated in the whois database.
The <sponsoring lir email> is the email to the sponsoring lir if there is a sponsoring lir, empty field otherwise.


How are we getting the results?
We take all inet(6)nums with status assigned pi and all autnums from the ripe database and for all those who have no abuse contact we check with rsng that it has status ENDUSER_APPROVEDDOCS and that it's not legacy space. Lastly we check with rsng to see if there's any sponsoring lir to be contacted.

Limitations?
* The inet(6)nums and autnums that has no org attribute are quietly disregarded - the resulting list is centered around organisation.
* Not all organisation in the resulting list have a sponsoring lir.
* There is no exclusion of organisations / contacts that has previously been mailed. Given that this program is only generating the list, not sending the mails, it's quite natural. However, CS needs to be able to keep track of which organisation was mailed, to what email address and when.

