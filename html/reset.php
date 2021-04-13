<?php
ini_set( 'display_errors', 1 );

include "DBHelper.php";

$db = new DBHelper();
$db->reset_event();
$db->reset_user();

echo "Reset Database";

?>
