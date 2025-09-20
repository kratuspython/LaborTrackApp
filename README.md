# LaborTrack

LaborTrack is a full-stack web application designed for local businesses in the community that do not have access to automated or dynamic employee tracking tools. The app allows business owners to easily manage employee information from a computer or smartphone, including personal details, contact information, job assignments, wage history, and more.

The main goal of building this application was not only to create something useful for small businesses but also to demonstrate my growth as a developer. Through this project, I challenged myself to learn and apply a new programming language (Java), work with both front-end and back-end development, explore frameworks, and integrate a database for real-world functionality.

1. Login your company credentials.
2. If you don't have a company account, create one (signup).
3. Successful, you will be redirect to the main/dashboard.
4. You will have many options avaliable at anytime: Home, logout, add-employee, delete-employee.
5. Add an employee to your company, fill the form information and click submit, then click home.
6. You now can see, emp picture, position, ID, and PunchIn button.
7. Click the punchIn button, auto will swap to PunchOut.
8. The right panel will give you insight of punch status: Name, ID, punchIn time, Duration, Punchout time: -- (default).
9. double click the employee card and you will be redirect to employeeInfo.
10. In employeeInfo you will have the entire employee information you include in the form but also the wage history.
11. Click home back again, you can delete the employee.

Therefore owner can add employees to his company, sees them in the middle panel of main. Punch in and out employees.
Check their punch status. Check their employee history including punchrecord history. Logout and login. The ability to have 
multiple companies as well.

The db is manage as following:

Company - Employees - punchRecords

A company can have multiple employees and employees can have multiple punchRecords.

All of that is possible by have three table, companies, employees and punchRecords.
Connected throught ids like company_id, employee_id.

employees will have a column company_id. Meaning this emp belongs to this company.
A punchRecord will have a column employee_id. Meaning this punchRecord belongs to this employee.

Everything cascade. punchRecord belongs to an employee that belongs to a company.
This will repead as much as how many emp a company have.
