module Demo
{
    interface UserToContextmanager
    {
        void userToLogin(string CumstomerName);
        string querytoCxtForItemDetails(string locationName, string user);
        string querytoCxtForItemsList(string locationName,string user);
    }
}