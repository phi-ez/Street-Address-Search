filename = input("Enter the file name: ")
inputaddress = open(filename, "r")
addresses = inputaddress.read().split("\n")
def Str(x):
	if x<10:
		return '0' + str(x)
	else:
		return str(x)
query = addresses.pop(10)
for i in range(len(addresses)):
	outputaddress = open('docs/doc'+Str(i+1)+'.txt',"w")
	outputaddress.write(addresses[i])
print(query)