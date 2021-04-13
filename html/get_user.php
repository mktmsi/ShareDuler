<?php
ini_set( 'display_errors', 1 );

include 'DBHelper.php';

$db =  new DBHelper();

$id = $_GET['id'];

$row = $db->get_user($id);

$jsonArray = array(
  'name' => $row['name']
);

header("Content-Type: application/json; charset=UTF-8");
echo json_encode($jsonArray, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE);

?>
