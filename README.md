# LaborTrackFinal


Labortrack is a full stack web application. The purpose of this app is for local buisness in the community that face difficulties having an automatic or dynamic way of tracking employees. Labortrack becomes an easy way for them to manage information, salary, wages, details, etc. Only with the use of an smarthphone or computer and internet.


1. You just simply login with a company.
2. If you don't have an account, you create an account (company) (signup).
3. You will be redirect to the main/dashboard.
4. At anytime you can click Home, Logout, add-employee, delete-employee(if has).
5. Add an employee, fill the form information, click submit, then click home.
6. In the main you will see the employee picture, position, ID and PunchIn button.
7. Once you click punchIn button, will swap to punchOut. 
8. The right panel will give you description of punch status: punchIn time, Duration, and punchOut:--(default).
9. If you click an emp card twice, you will be redirect to employeeInfo.
10. In employeeInfo you will have the entire description of the employee including punch History.
11. You also have the ability to delete employees from your company.


DB is manage the following way:

Company - Employees - PunchRecords (individually)
A company can have multiple employees. Each employee will be link to an  punchRecords table.
Everything is connect by the following: companyId, EmployeeId.

Company will have a company_id.
Employee table will have a company_id column. Meaning this employee belongs to this company.
Punchrecord table will have a employee_id column. Meaning this punch belongs to the following employee.

Everything cascade. punchrecod belongs to an employee which belong to a company. 
This will repead as much as employees a company have.

