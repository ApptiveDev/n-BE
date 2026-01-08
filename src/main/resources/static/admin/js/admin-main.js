// 전역 변수
let currentTab = 'approval'; // 'approval' 또는 'matching'
let selectedMemberId = null;
let selectedFemaleMemberId = null;
let selectedMaleMemberIds = [];
let matchingCandidates = [];

// API Base URL
const API_BASE_URL = '/admin/members';

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    loadApprovalMembers();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // 탭 전환
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            const tabType = this.dataset.tab;
            switchTab(tabType);
        });
    });

    // 검색
    document.getElementById('search-btn').addEventListener('click', function() {
        loadApprovalMembers();
    });

    document.getElementById('search-input').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            loadApprovalMembers();
        }
    });

    // 필터 초기화
    document.getElementById('reset-filter').addEventListener('click', function() {
        document.getElementById('search-input').value = '';
        loadApprovalMembers();
    });

    // 로그아웃
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    // 모달 관련
    document.querySelectorAll('.modal-close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            modal.classList.remove('show');
        });
    });

    document.getElementById('modal-cancel').addEventListener('click', function() {
        document.getElementById('status-modal').classList.remove('show');
    });

    document.getElementById('modal-confirm').addEventListener('click', confirmStatusChange);

    // 매칭 관련
    document.getElementById('back-to-female-list').addEventListener('click', function() {
        showFemaleList();
    });

    document.getElementById('select-all-males').addEventListener('change', function() {
        const checkboxes = document.querySelectorAll('#matching-candidates-body input[type="checkbox"]');
        checkboxes.forEach(cb => {
            cb.checked = this.checked;
            if (this.checked) {
                const memberId = parseInt(cb.dataset.memberId);
                if (!selectedMaleMemberIds.includes(memberId)) {
                    selectedMaleMemberIds.push(memberId);
                }
            } else {
                selectedMaleMemberIds = [];
            }
        });
        updateMatchingButton();
    });

    document.getElementById('create-matching-btn').addEventListener('click', function() {
        if (selectedMaleMemberIds.length === 3) {
            openMatchingConfirmModal();
        }
    });

    document.getElementById('matching-cancel').addEventListener('click', function() {
        document.getElementById('matching-confirm-modal').classList.remove('show');
    });

    document.getElementById('matching-confirm').addEventListener('click', createMatching);

    // 모달 외부 클릭 시 닫기
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('show');
            }
        });
    });
}

// 탭 전환
function switchTab(tabType) {
    currentTab = tabType;
    
    // 탭 활성화 상태 변경
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
        if (tab.dataset.tab === tabType) {
            tab.classList.add('active');
        }
    });

    // 콘텐츠 표시/숨김
    document.getElementById('approval-tab-content').classList.toggle('active', tabType === 'approval');
    document.getElementById('matching-tab-content').classList.toggle('active', tabType === 'matching');
    document.getElementById('matching-list-tab-content').classList.toggle('active', tabType === 'matching-list');

    // 타이틀 변경
    const titleMap = {
        'approval': '유저 승인',
        'matching': '유저 매칭',
        'matching-list': '매칭 확인'
    };
    document.getElementById('current-tab-title').textContent = titleMap[tabType] || '유저 승인';

    // 데이터 로드
    if (tabType === 'approval') {
        loadApprovalMembers();
    } else if (tabType === 'matching') {
        loadConnectingFemaleMembers();
    } else if (tabType === 'matching-list') {
        loadMatchingList();
    }
}

// ==================== 유저 승인 탭 ====================

/**
 * Use Case 1: 승인 대기 유저 목록 조회
 */
async function loadApprovalMembers() {
    try {
        const keyword = document.getElementById('search-input').value.trim();
        const url = keyword 
            ? `${API_BASE_URL}/pending-approval?keyword=${encodeURIComponent(keyword)}`
            : `${API_BASE_URL}/pending-approval`;
        
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('승인 대기 유저 목록 조회 실패');
        }
        
        const members = await response.json();
        renderApprovalTable(members);
        updateTotalCount(members.length);
    } catch (error) {
        console.error('승인 대기 유저 목록 조회 오류:', error);
        showError('승인 대기 유저 목록을 불러오는데 실패했습니다.');
    }
}

/**
 * 승인 대기 유저 테이블 렌더링
 */
function renderApprovalTable(members) {
    const tbody = document.getElementById('approval-table-body');
    
    if (members.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="7">
                    <div class="empty-state">
                        <p>조회된 회원이 없습니다.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = members.map(member => `
        <tr>
            <td>${member.id}</td>
            <td>
                <a href="#" class="member-name-link" onclick="openMemberDetail(${member.id}); return false;">
                    ${member.name}
                </a>
            </td>
            <td>${member.email}</td>
            <td>
                <span class="status-badge ${getStatusClass(member.status)}">
                    ${getStatusText(member.status)}
                </span>
            </td>
            <td>${formatDate(member.createdAt)}</td>
            <td>${member.residenceArea || '-'}</td>
            <td>
                <button class="action-btn" onclick="openStatusModal(${member.id}, '${member.name}', '${member.status}')">
                    상태 변경
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Use Case 1: 유저 상세 정보 조회
 */
async function openMemberDetail(memberId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${memberId}`);
        if (!response.ok) {
            throw new Error('유저 상세 정보 조회 실패');
        }
        
        const member = await response.json();
        renderMemberDetail(member);
        document.getElementById('member-detail-modal').classList.add('show');
    } catch (error) {
        console.error('유저 상세 정보 조회 오류:', error);
        showError('유저 상세 정보를 불러오는데 실패했습니다.');
    }
}

/**
 * 유저 상세 정보 렌더링
 */
function renderMemberDetail(member) {
    const content = document.getElementById('member-detail-content');
    const footer = document.getElementById('member-detail-footer');
    
    content.innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
            <div>
                <h4 style="margin-bottom: 10px; color: #667eea;">기본 정보</h4>
                <p><strong>ID:</strong> ${member.id}</p>
                <p><strong>이름:</strong> ${member.name}</p>
                <p><strong>이메일:</strong> ${member.email}</p>
                <p><strong>상태:</strong> <span class="status-badge ${getStatusClass(member.status)}">${getStatusText(member.status)}</span></p>
                <p><strong>성별:</strong> ${member.gender === 'JAPANESE_FEMALE' ? '일본 여성' : '한국 남성'}</p>
                <p><strong>등록일:</strong> ${formatDate(member.createdAt)}</p>
            </div>
            <div>
                <h4 style="margin-bottom: 10px; color: #667eea;">신체 정보</h4>
                <p><strong>키:</strong> ${member.height || '-'} cm</p>
                <p><strong>몸무게:</strong> ${member.weight || '-'} kg</p>
                <p><strong>거주지역:</strong> ${member.residenceArea || '-'}</p>
            </div>
            <div>
                <h4 style="margin-bottom: 10px; color: #667eea;">기타 정보</h4>
                <p><strong>흡연:</strong> ${member.smokingStatus ? getSmokingText(member.smokingStatus) : '-'}</p>
                <p><strong>음주:</strong> ${member.drinkingFrequency ? getDrinkingText(member.drinkingFrequency) : '-'}</p>
                <p><strong>종교:</strong> ${member.religion ? getReligionText(member.religion) : '-'}</p>
                ${member.religionOther ? `<p><strong>종교 기타:</strong> ${member.religionOther}</p>` : ''}
                <p><strong>학력:</strong> ${member.education ? getEducationText(member.education) : '-'}</p>
                <p><strong>자산:</strong> ${member.asset ? getAssetText(member.asset) : '-'}</p>
            </div>
            <div>
                <h4 style="margin-bottom: 10px; color: #667eea;">추가 정보</h4>
                <p><strong>기타 정보:</strong> ${member.otherInfo || '-'}</p>
                ${member.profileImageUrl ? `<p><strong>프로필 이미지:</strong> <a href="${member.profileImageUrl}" target="_blank">보기</a></p>` : ''}
            </div>
        </div>
    `;
    
    // 승인 대기 상태인 경우에만 상태 변경 버튼 표시
    if (member.status === 'PENDING_APPROVAL') {
        footer.innerHTML = `
            <button class="btn btn-primary" onclick="openStatusModal(${member.id}, '${member.name}', '${member.status}')">
                상태 변경
            </button>
        `;
    } else {
        footer.innerHTML = '';
    }
}

/**
 * Use Case 2: 상태 변경 모달 열기
 */
function openStatusModal(memberId, memberName, currentStatus) {
    selectedMemberId = memberId;
    document.getElementById('modal-member-name').textContent = memberName;
    
    // 승인 대기 상태인 경우 승인완료 또는 블랙유저 선택 가능
    const statusSelect = document.getElementById('new-status');
    statusSelect.innerHTML = '';
    if (currentStatus === 'PENDING_APPROVAL') {
        statusSelect.innerHTML = `
            <option value="APPROVED">승인완료</option>
            <option value="BLACKLISTED">블랙 유저</option>
        `;
    }
    
    document.getElementById('status-modal').classList.add('show');
}

/**
 * Use Case 2: 상태 변경 확인
 */
async function confirmStatusChange() {
    if (!selectedMemberId) return;
    
    const newStatus = document.getElementById('new-status').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/${selectedMemberId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: newStatus })
        });
        
        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: '상태 변경 실패' }));
            throw new Error(error.message || '상태 변경 실패');
        }
        
        showSuccess('상태가 변경되었습니다.');
        document.getElementById('status-modal').classList.remove('show');
        selectedMemberId = null;
        
        // 목록 새로고침
        if (currentTab === 'approval') {
            loadApprovalMembers();
        }
    } catch (error) {
        console.error('상태 변경 오류:', error);
        showError(error.message || '상태 변경에 실패했습니다.');
    }
}

// ==================== 유저 매칭 탭 ====================

/**
 * Use Case 3: 승인완료 상태 여성 유저 목록 조회
 */
async function loadConnectingFemaleMembers() {
    try {
        const response = await fetch(`${API_BASE_URL}/connecting/females`);
        if (!response.ok) {
            throw new Error('승인완료 여성 유저 목록 조회 실패');
        }
        
        const members = await response.json();
        renderFemaleTable(members);
        updateTotalCount(members.length);
    } catch (error) {
        console.error('승인완료 여성 유저 목록 조회 오류:', error);
        showError('승인완료 여성 유저 목록을 불러오는데 실패했습니다.');
    }
}

/**
 * 승인완료 여성 유저 테이블 렌더링
 */
function renderFemaleTable(members) {
    const tbody = document.getElementById('female-table-body');
    
    if (members.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="6">
                    <div class="empty-state">
                        <p>조회된 회원이 없습니다.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = members.map(member => `
        <tr>
            <td>${member.id}</td>
            <td>
                <a href="#" class="member-name-link" onclick="selectFemaleMember(${member.id}, '${member.name}'); return false;">
                    ${member.name}
                </a>
            </td>
            <td>${member.email}</td>
            <td>${formatDate(member.createdAt)}</td>
            <td>${member.residenceArea || '-'}</td>
            <td>
                <button class="action-btn" onclick="selectFemaleMember(${member.id}, '${member.name}')">
                    매칭 후보 보기
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Use Case 4: 여성 유저 선택 및 매칭 후보 조회
 */
async function selectFemaleMember(femaleId, femaleName) {
    selectedFemaleMemberId = femaleId;
    document.getElementById('selected-female-name').textContent = femaleName;
    
    try {
        showLoading('매칭 후보를 조회하는 중...');
        
        const response = await fetch(`${API_BASE_URL}/${femaleId}/matching-candidates`);
        if (!response.ok) {
            throw new Error('매칭 후보 조회 실패');
        }
        
        matchingCandidates = await response.json();
        
        if (matchingCandidates.length === 0) {
            showError('현재 조건에 맞는 남성 유저가 없습니다.');
            return;
        }
        
        renderMatchingCandidates(matchingCandidates);
        showMatchingCandidatesSection();
        hideLoading();
    } catch (error) {
        console.error('매칭 후보 조회 오류:', error);
        showError('매칭 후보를 불러오는데 실패했습니다.');
        hideLoading();
    }
}

/**
 * 매칭 후보 테이블 렌더링
 */
function renderMatchingCandidates(candidates) {
    const tbody = document.getElementById('matching-candidates-body');
    selectedMaleMemberIds = [];
    
    tbody.innerHTML = candidates.map(candidate => `
        <tr>
            <td>
                <input type="checkbox" 
                       data-member-id="${candidate.memberId}"
                       onchange="toggleMaleSelection(${candidate.memberId}, this.checked)">
            </td>
            <td>${candidate.memberId}</td>
            <td>${candidate.name}</td>
            <td>${candidate.email}</td>
            <td>${candidate.height || '-'} cm</td>
            <td>${candidate.weight || '-'} kg</td>
            <td>${candidate.residenceArea || '-'}</td>
            <td>
                <span class="badge" style="background: ${candidate.matchingCount > 0 ? '#4299e1' : '#a0aec0'}; color: white; padding: 4px 12px; border-radius: 12px; font-weight: 600;">
                    ${candidate.matchingCount || 0}개
                </span>
            </td>
            <td>
                <span class="score-badge" style="background: ${candidate.scoreColor || getScoreColor(candidate.matchingScore)}; color: white; padding: 4px 12px; border-radius: 12px; font-weight: 600;">
                    ${candidate.matchingScore.toFixed(1)}점
                </span>
            </td>
        </tr>
    `).join('');
    
    updateMatchingButton();
}

/**
 * 남성 유저 선택 토글
 */
function toggleMaleSelection(memberId, checked) {
    if (checked) {
        if (selectedMaleMemberIds.length >= 3) {
            event.target.checked = false;
            showError('남성 유저는 최대 3명까지만 선택할 수 있습니다.');
            return;
        }
        if (!selectedMaleMemberIds.includes(memberId)) {
            selectedMaleMemberIds.push(memberId);
        }
    } else {
        selectedMaleMemberIds = selectedMaleMemberIds.filter(id => id !== memberId);
    }
    updateMatchingButton();
}

/**
 * 매칭 버튼 상태 업데이트
 */
function updateMatchingButton() {
    const btn = document.getElementById('create-matching-btn');
    const count = document.getElementById('selected-male-count');
    count.textContent = selectedMaleMemberIds.length;
    
    if (selectedMaleMemberIds.length === 3) {
        btn.disabled = false;
        btn.style.opacity = '1';
    } else {
        btn.disabled = true;
        btn.style.opacity = '0.5';
    }
}

/**
 * 매칭 후보 섹션 표시
 */
function showMatchingCandidatesSection() {
    document.getElementById('female-list-section').style.display = 'none';
    document.getElementById('matching-candidates-section').style.display = 'block';
}

/**
 * 여성 유저 목록으로 돌아가기
 */
function showFemaleList() {
    document.getElementById('female-list-section').style.display = 'block';
    document.getElementById('matching-candidates-section').style.display = 'none';
    selectedFemaleMemberId = null;
    selectedMaleMemberIds = [];
    matchingCandidates = [];
}

/**
 * Use Case 5: 매칭 확인 모달 열기
 */
function openMatchingConfirmModal() {
    const femaleName = document.getElementById('selected-female-name').textContent;
    const selectedMales = matchingCandidates.filter(c => selectedMaleMemberIds.includes(c.memberId));
    
    document.getElementById('confirm-female-name').textContent = femaleName;
    document.getElementById('confirm-male-names').innerHTML = `
        <strong>선택된 남성 유저:</strong>
        <ul style="margin-top: 10px; padding-left: 20px;">
            ${selectedMales.map(m => `<li>${m.name} (${m.matchingScore.toFixed(1)}점)</li>`).join('')}
        </ul>
    `;
    
    document.getElementById('matching-confirm-modal').classList.add('show');
}

/**
 * Use Case 5: 매칭 생성
 */
async function createMatching() {
    if (selectedMaleMemberIds.length !== 3) {
        showError('남성 유저 3명을 선택해주세요.');
        return;
    }
    
    try {
        showLoading('매칭을 생성하는 중...');
        
        const response = await fetch(`${API_BASE_URL}/matching`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                femaleMemberId: selectedFemaleMemberId,
                maleMemberIds: selectedMaleMemberIds
            })
        });
        
        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: '매칭 생성 실패' }));
            throw new Error(error.message || '매칭 생성 실패');
        }
        
        hideLoading();
        showSuccess('매칭이 성공적으로 생성되었습니다. (승인완료 → 연결중)');
        document.getElementById('matching-confirm-modal').classList.remove('show');
        
        // 목록 새로고침
        showFemaleList();
        loadConnectingFemaleMembers();
    } catch (error) {
        console.error('매칭 생성 오류:', error);
        hideLoading();
        showError(error.message || '매칭 생성에 실패했습니다.');
    }
    
}


// Use Case 6: 매칭 목록 조회
async function loadMatchingList() {
    try {
        const response = await fetch(`${API_BASE_URL}/matchings`);
        if (!response.ok) {
            throw new Error('매칭 목록 조회 실패');
        }
        
        const matchings = await response.json();
        renderMatchingListTable(matchings);
        updateTotalCount(matchings.length);
    } catch (error) {
        console.error('매칭 목록 조회 오류:', error);
        showError('매칭 목록을 불러오는데 실패했습니다.');
    }
}
//매칭 목록 렌더링
function renderMatchingListTable(matchings) {
    const tbody = document.getElementById('matching-list-table-body');
    
    if (matchings.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="6">
                    <div class="empty-state">
                        <p>조회된 매칭이 없습니다.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = matchings.map(matching => `
        <tr>
            <td>${matching.matchingId}</td>
            <td>
                <a href="#" class="member-name-link" onclick="openMemberDetail(${matching.femaleMemberId}); return false;">
                    ${matching.femaleName}
                </a>
                <br>
                <small style="color: #718096;">${matching.femaleEmail}</small>
            </td>
            <td>
                ${matching.maleMembers.map((male, index) => `
                    <div style="margin-bottom: 8px;">
                        <a href="#" class="member-name-link" onclick="openMemberDetail(${male.memberId}); return false;">
                            ${male.name}
                        </a>
                        <br>
                        <small style="color: #718096;">${male.email}</small>
                        <br>
                        <small style="color: #a0aec0;">순서: ${male.order}</small>
                    </div>
                `).join('')}
            </td>
            <td>${formatDate(matching.createdAt)}</td>
        </tr>
    `).join('');
}
// ==================== 유틸리티 함수 ====================

function getStatusClass(status) {
    const statusMap = {
        'PENDING_APPROVAL': 'status-pending',
        'APPROVED': 'status-approved',
        'CONNECTING': 'status-connecting',
        'CONNECTED': 'status-connected',
        'BLACKLISTED': 'status-blacklisted'
    };
    return statusMap[status] || '';
}

function getStatusText(status) {
    const statusMap = {
        'PENDING_APPROVAL': '승인대기',
        'APPROVED': '승인완료',
        'CONNECTING': '연결중',
        'CONNECTED': '연결됨',
        'BLACKLISTED': '블랙 유저'
    };
    return statusMap[status] || status;
}

function getSmokingText(status) {
    const map = { 'SMOKER': '흡연', 'NON_SMOKER': '비흡연' };
    return map[status] || status;
}

function getDrinkingText(frequency) {
    const map = {
        'LESS_THAN_ONCE_A_WEEK': '주 1회 미만',
        'ONCE_A_WEEK': '주 1회',
        'TWICE_A_WEEK': '주 2회',
        'MORE_THAN_THREE_TIMES_A_WEEK': '주 3회 이상'
    };
    return map[frequency] || frequency;
}

function getReligionText(religion) {
    const map = {
        'NONE': '무교',
        'BUDDHISM': '불교',
        'CHRISTIANITY': '기독교',
        'CATHOLICISM': '천주교',
        'SHINTO': '신토',
        'OTHER': '기타'
    };
    return map[religion] || religion;
}

function getEducationText(education) {
    const map = {
        'HIGH_SCHOOL': '고등학교 졸업',
        'ASSOCIATE_DEGREE': '전문학사',
        'BACHELOR_DEGREE': '학사',
        'MASTER_DEGREE': '석사',
        'DOCTORATE_DEGREE': '박사'
    };
    return map[education] || education;
}

function getAssetText(asset) {
    const map = {
        'UNDER_100M': '1억 미만',
        'BETWEEN_100M_300M': '1억-3억',
        'BETWEEN_300M_500M': '3억-5억',
        'BETWEEN_500M_1B': '5억-10억',
        'OVER_1B': '10억 초과'
    };
    return map[asset] || asset;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}


/**
 * 매칭 점수에 따른 그라데이션 색상 (5점 단위)
 * 100점(어두운 초록) → 50점(진한 빨강)
 * 50점 미만은 고정 빨강색
 */
function getScoreColor(score) {
    if (score === 100) return '#1B5E20';  // 100: 어두운 초록
    if (score >= 95) return '#2E7D32';    // 95: 진한 초록
    if (score >= 90) return '#388E3C';    // 90: 초록
    if (score >= 85) return '#4CAF50';    // 85: 밝은 초록
    if (score >= 80) return '#8BC34A';    // 80: 연두
    if (score >= 75) return '#CDDC39';    // 75: 라임
    if (score >= 70) return '#FFEB3B';    // 70: 노랑
    if (score >= 65) return '#FFC107';    // 65: amber
    if (score >= 60) return '#FF9800';    // 60: 주황
    if (score >= 55) return '#FF5722';    // 55: 진한 주황
    if (score >= 50) return '#D32F2F';    // 50: 진한 빨강
    return '#B71C1C';                     // 0-50: 더 진한 빨강 (고정)
}

function updateTotalCount(count) {
    document.getElementById('total-count').textContent = count;
}

function showSuccess(message) {
    alert('✅ ' + message);
}

function showError(message) {
    alert('❌ ' + message);
}

function showLoading(message) {
    // 간단한 로딩 표시 (필요시 개선 가능)
    console.log('Loading: ' + message);
}

function hideLoading() {
    console.log('Loading finished');
}

// 로그아웃 처리
async function handleLogout() {
    if (!confirm('로그아웃 하시겠습니까?')) {
        return;
    }

    try {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/admin/logout';
        document.body.appendChild(form);
        form.submit();
    } catch (error) {
        console.error('로그아웃 오류:', error);
        showError('로그아웃 처리 중 오류가 발생했습니다.');
    }
}