function selectAvatarAndSubmit(element) {
    const avatar = element.getAttribute('data-avatar');

    document.getElementById('selectedAvatar').value = avatar;

    document.getElementById('avatarForm').submit();
}
