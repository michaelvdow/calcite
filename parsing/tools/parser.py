import csv
import re

testFile = open("./tests.txt", "r")
text = testFile.read()
regex = 'sql = \"(.+?\n?)+?\";'
nextIndex = 0
calls = []
while (True):
    m = re.search(regex, text[nextIndex :])
    if (m == None) :
        break
    nextIndex += m.span()[1]
    call = m.group()
    if "create procedure" not in call and "^" not in call:
        calls.append(call)

updatedCalls = []
for call in calls:
    currentCall = call[7:]
    replace = '("\n.+?\+ ")'
    currentCall = re.sub(replace, '', currentCall)
    currentCall = re.sub('";', '', currentCall)
    if currentCall.startswith('"'):
        currentCall = currentCall[1:]
    if currentCall.endswith('"'):
        currentCall = currentCall[:len(currentCall) - 1]
    print(currentCall)
    updatedCalls.append(currentCall)

with open('test2.csv', 'w', newline='\n') as csvfile:
    spamwriter = csv.writer(csvfile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    spamwriter.writerow("Queries")
    for call in updatedCalls:
        query = "create procedure foo () " + call
        spamwriter.writerow([query])
