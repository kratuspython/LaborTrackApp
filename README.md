# LaborTrackFinal


LaborTrack is full stack web application. The purpose of this app is for local buisness in the community that has not access to an automatic or dynamic employee tracking. This web application will be beneficial because with access to a computer or smartphone, owner can easily access employee Personal information, contact Information, Job Details, Wage history, etc.

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
